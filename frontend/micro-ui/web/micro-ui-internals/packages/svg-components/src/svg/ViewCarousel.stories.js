import React from "react";
import { ViewCarousel } from "./ViewCarousel";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "ViewCarousel",
  component: ViewCarousel,
};

export const Default = () => <ViewCarousel />;
export const Fill = () => <ViewCarousel fill="blue" />;
export const Size = () => <ViewCarousel height="50" width="50" />;
export const CustomStyle = () => <ViewCarousel style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <ViewCarousel className="custom-class" />;
export const Clickable = () => <ViewCarousel onClick={()=>console.log("clicked")} />;

const Template = (args) => <ViewCarousel {...args}/>;

export const Playground = Template.bind({});
Playground.args = {
  className: 'custom-class',
  style:{ border: "3px solid green" }
};
