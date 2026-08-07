import React from "react";
import { ArrowForwardIos } from "./ArrowForwardIos";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "ArrowForwardIos",
  component: ArrowForwardIos,
};

export const Default = () => <ArrowForwardIos />;
export const Fill = () => <ArrowForwardIos fill="blue" />;
export const Size = () => <ArrowForwardIos height="50" width="50" />;
export const CustomStyle = () => <ArrowForwardIos style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <ArrowForwardIos className="custom-class" />;
export const Clickable = () => <ArrowForwardIos onClick={()=>console.log("clicked")} />;

const Template = (args) => <ArrowForwardIos {...args}/>;

export const Playground = Template.bind({});
Playground.args = {
  className: 'custom-class',
  style:{ border: "3px solid green" }
};
