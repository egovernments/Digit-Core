import React from "react";
import { AddChart } from "./AddChart";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "AddChart",
  component: AddChart,
};

export const Default = () => <AddChart />;
export const Fill = () => <AddChart fill="blue" />;
export const Size = () => <AddChart height="50" width="50" />;
export const CustomStyle = () => <AddChart style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <AddChart className="custom-class" />;
export const Clickable = () => <AddChart onClick={()=>console.log("clicked")} />;

const Template = (args) => <AddChart {...args}/>;

export const Playground = Template.bind({});
Playground.args = {
  className: 'custom-class',
  style:{ border: "3px solid green" }
};
