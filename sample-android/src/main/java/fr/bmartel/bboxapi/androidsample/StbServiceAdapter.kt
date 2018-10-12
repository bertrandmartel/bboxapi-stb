package fr.bmartel.bboxapi.androidsample

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView

class StbServiceAdapter(
        context: Context,
        var list: List<StbServiceItem>
) : BaseAdapter() {

    private val layoutInflater: LayoutInflater = LayoutInflater.from(context)

    override fun getCount(): Int {
        return list.size
    }

    override fun getItem(position: Int): Any {
        return position
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val viewHolder: ViewHolder
        var view: View? = convertView

        if (view == null) {
            view = layoutInflater.inflate(R.layout.stb_service_item, parent, false)

            viewHolder = ViewHolder(
                    view.findViewById(R.id.ip),
                    view.findViewById(R.id.stb_img),
                    view.findViewById(R.id.img_principal)
            )
            view?.tag = viewHolder
        } else {
            viewHolder = view.tag as ViewHolder
        }
        viewHolder.textView.text = "${list[position].ip}:${list[position].port}"
        viewHolder.imageView.setImageResource(list[position].imgRes)
        if (list[position].principal) {
            viewHolder.principalImg.setImageResource(R.drawable.ic_action_star_10)
        } else {
            viewHolder.principalImg.setImageResource(0)
        }
        return view!!
    }

    data class ViewHolder(val textView: TextView, val imageView: ImageView, val principalImg: ImageView)
}